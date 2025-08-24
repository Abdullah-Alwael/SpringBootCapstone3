# SpringBootCapstone3
A Planting and Landscaping Service Management System

## Extra end-points made by Abdullah
1. Pay: integrate Moyasar APi and allow users to pay
2. View payment status: returns the status of the payment from Moyasar API
3. Payment callback: allow Moyasar to call the end-point and update payment status after user performs payment
4. Get vendor contracts between two dates: check contracts in given date range
5. Get overdue contracts: list accepted contracts that are unpaid for 15 days or more
6. Get top five vendors: based on the number of offers accepted, returns best-selling vendors
7. Get almost expired contracts: return a list of contracts that are about to expire for a given vendor
8. Renew expired contract: in case a user (organization) would like to request the same project again
9. Get contracts statistics: allow a vendor to have a dashboard of his contracts statistics like total contracts made, number of soon expiring contracts, number of expired contracts and total amount profited (revenue)
10. Sync all contracts status from Moyasar API: allows for updating the data records to match Moyasar data and remove inconsistency

In addition to:
1. Creating Invoice CRUD (removed later)
2. Creating Contract CRUD (Reconstructed by Hatem)

## How to contribute to the project development:
1. clone the project:

   `git clone https://github.com/Abdullah-Alwael/SpringBootCapstone3.git`

2. create a branch with your name:

   `git checkout -b YOUR-NAME`

   - Note: replace YOUR-NAME with your name (preferably all small letters)
   - This will create a branch for you and move to it automatically.

3. setup your remote branch:

   `git push --set-upstream origin YOUR-NAME`

4. Create database in dataGrip:
   `create database landscaping_system;`

   `use landscaping_system;`

       - Note: start MySQL server

