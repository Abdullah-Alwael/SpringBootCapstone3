package com.spring.boot.springbootcapstone3.Controller;

import com.spring.boot.springbootcapstone3.API.ApiResponse;
import com.spring.boot.springbootcapstone3.Model.PaymentRequest;
import com.spring.boot.springbootcapstone3.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // extra by abdullah
    @PostMapping("/pay/{contractId}")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest, @PathVariable Integer contractId) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.processPayment(contractId, paymentRequest));
    }

    // extra by abdullah
    @GetMapping("/get-status/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentId) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.getPaymentStatus(paymentId));
    }

    // extra by abdullah
    @GetMapping("/callback/{contractId}")
    public ResponseEntity<?> callback(@PathVariable Integer contractId
            , @RequestParam(name = "id") String transaction_id
            , @RequestParam(name = "status") String status
            , @RequestParam(name = "amount") String amount
            , @RequestParam(name = "message") String message) {

        paymentService.updateStatus(contractId, transaction_id, status);

        // example callback url:
        // http://localhost:8080/api/v1/payment/callback/1

        // the following parameters will be attached to the above link from Moyasar API:
        // ?id=074ab20f-ffeb-4d36-a103-6237b9629191
        // &status=paid
        // &amount=50000
        // &message=APPROVED

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse("Payment is "+message
                        +" for the amount of "+Float.parseFloat(amount)/100 // return the original value
                        +" with status: "+status));

    }

    // extra by abdullah
    @PutMapping("/sync-status")
    public ResponseEntity<?> syncContractStatus(){
        paymentService.syncContractStatus();
        return ResponseEntity.ok(new ApiResponse("Sync successful with no inconsistency in the records"));
    }

}
