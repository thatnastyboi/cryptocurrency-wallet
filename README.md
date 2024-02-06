# Cryptocurrency Wallet Manager

The Cryptocurrency Wallet Manager is a command-line tool designed for users to purchase, store, and manage their cryptocurrency wallets. It facilitates basic operations including depositing funds, trading cryptocurrencies, accessing wallet details, managing accounts, and retrieving real-time pricing data for various cryptocurrencies via the CoinAPI.

### Functionalities

- __help__ - Displays information about the available commands, their parameters, and when the commands can be used.
- __register  {name}  {password}__ - Registers a new account in the database.
- __login  {name}  {password}__ - Logs in an existing account in the database.
- __logout__ - Logs out of the currently logged account.
- __disconnect__ - Disconnects current account from the server
- __deposit-money  {amount}__ - Deposits given amount of money in the wallet of the currently logged account.
- __list-offerings__ - Lists every available for purchase cryptocurrency.
- __buy  {asset_code}  {amount}__ - Purchases quantity of given cryptocurrency by given amount of money.
- __sell  {asset_code}__ - Sells cryptocurrency available in the wallet of the currently logged account.
- __get-wallet-summary__ - Shows information about the wallet of the currently logged account, including current balance, which cryptocurrency they have in their wallet, as well as their quantity.
- __get-wallet-overall-summary__ - Shows information about the overall winnings of the currently logged account.
- __shutdown__ - Shuts the server down.
