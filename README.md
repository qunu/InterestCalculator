##Interest calculation application

Rest endpoints exposed :

 - processAccountOpening
 - processAccountEndOfDayBalances
 - calculateMonthlyInterest
 - processAccountClosure
 
###processAccountOpening
Find if account already exists
If account exists that is not active then set to active and set Opening date to received Opening date
If account exists that is active then return (Job done)
If no account exists then create new account

###processAccountEndOfDayBalances
Calculates the daily interest rate using interest from property file
Updates Received Balances  to the Database
Get active accounts where Balance greater than 0 and calculate daily interest then adding it to the interest stored in the Account
Create Account Interest record for audit and monthly calculations

###calculateMonthlyInterest
Use RequestParam to get month
Get all Account Interest from 'audit' table for the month received in the request per account and sums up the interests
Returns a list of Monthly Interest for all active accounts in data-store
eg: http://localhost:8080/calculateMonthlyInterest?month=2021-09

###processAccountClosure
Find Account and set Active to false and Close Date to now if Account still Active

---

###Notes:

 - The application is using an in memory H2 database
 - Have not used Interface+Impl coding practice for this small assignment (Can if needed)
 - Unit test are missing for some scenarios
 - The supplied Json in email was malformed, I corrected it for testing