package com.spring.boot.springbootcapstone3.Service;

import com.spring.boot.springbootcapstone3.API.ApiException;
import com.spring.boot.springbootcapstone3.Model.Contract;
import com.spring.boot.springbootcapstone3.Model.PaymentRequest;
import com.spring.boot.springbootcapstone3.Repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService { // created by Abdullah Alwael
    @Value("${moyasar.api.key}")
    private String apiKey;

    private static final String MOYASAR_API_URL = "https://api.moyasar.com/v1/payments/";
    private final ContractRepository contractRepository;

    public ResponseEntity<String> processPayment(Integer contractId, PaymentRequest paymentRequest) {

        Contract contract = contractRepository.findContractById(contractId);

        if (contract == null){
            throw new ApiException("Contract not found");
        }

        if(contract.getStatus().equalsIgnoreCase("PAID")){
            throw new ApiException("Error, the contract invoice was already paid!");
        }

        String callBackUrl = "http://localhost:8080/api/v1/payment/callback/"+contract.getId();

        String requestBody = String.format(
                "source[type]=card&source[name]=%s&source[number]=%s&source[cvc]=%s&" +
                        "source[month]=%s&source[year]=%s&amount=%d&currency=%s&" +
                        "callback_url=%s",
                paymentRequest.getName(),
                paymentRequest.getNumber(),
                paymentRequest.getCvc(),
                paymentRequest.getMonth(),
                paymentRequest.getYear(),
                (int) ((contract.getPrice()+contract.getPrice()*0.2)*100),
                // must convert to the smallest currency unit, to add the halala
                // and add the service commission of 20%
                paymentRequest.getCurrency(),
                callBackUrl
        );

        // setting up the HTTP header options
        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(MOYASAR_API_URL,
                HttpMethod.POST, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    public String getPaymentStatus(String paymentId){
        // headers:
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // create Request:

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // call Moyasar API:
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL + paymentId, HttpMethod.GET, entity, String.class
        );

        // return the response:
        return response.getBody();
    }

    public void updateStatus(Integer contractId, String transaction_id, String status){

        Contract contract = contractRepository.findContractById(contractId);

        if (contract == null){
            throw new ApiException("Contract not found");
        }

        contract.setStatus(status.toUpperCase());
        contract.setTransactionId(transaction_id);

        contractRepository.save(contract);
    }

    public void syncContractStatus(){
        List<Contract> contracts = contractRepository.giveMeContractsWithTransactionsNotEmpty();

        int inconsistencyCount = 0;

        for (Contract c: contracts){
            String response = getPaymentStatus(c.getTransactionId());
            JSONObject MoyasarPaymentStatus = new JSONObject(response);

            // sample from Moyasar
            // {"id":"6bef16fa-37a1-42af-ae0b-3b52e9c441f2","status":"paid","amount":3000000,"fee":0,"currency":"SAR","refunded":0,"refunded_at":null,"captured":0,"captured_at":null,"voided_at":null,"description":null,"amount_format":"30,000.00 SAR","fee_format":"0.00 SAR","refunded_format":"0.00 SAR","captured_format":"0.00 SAR","invoice_id":null,"ip":"37.224.52.18","callback_url":"http://localhost:8080/api/v1/payment/callback/1","created_at":"2025-08-21T11:18:26.045Z","updated_at":"2025-08-21T11:18:35.243Z","metadata":null,"source":{"type":"creditcard","company":"mada","name":"Abdullah Test","number":"4201-32XX-XXXX-1010","gateway_id":"moyasar_cc_462nQGbVcdAZ4eQ8dxAoDVA","reference_number":"992523342283","token":null,"message":"APPROVED","transaction_url":null,"response_code":"00","authorization_code":"001663","issuer_name":"MOYASAR PAY SANDBOX","issuer_country":"SA","issuer_card_type":"credit","issuer_card_category":"PREPAID"}}

            String status = MoyasarPaymentStatus.getString("status");

            if (!status.equalsIgnoreCase(c.getStatus())){
                inconsistencyCount++;

                c.setStatus(status.toUpperCase());
                contractRepository.save(c);
            }

        }

        if (inconsistencyCount !=0){
            throw new ApiException("Contract status synced but found "+inconsistencyCount+" inconsistency records");
        }

    }


}
