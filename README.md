# Cryptocurrency Wallet Manager

The Cryptocurrency Wallet Manager is a command-line tool designed for users to purchase, store, and manage their cryptocurrency wallets. It facilitates basic operations including depositing funds, trading cryptocurrencies, accessing wallet details, managing accounts, and retrieving real-time pricing data for various cryptocurrencies via the CoinAPI.

### Functionalities

- help
  - Displays information about the available commands, their parameters, and when the commands can be used.
- register <name> <password>
  - Registers a new account in the database.
- login <name> <password>
  - Logs in an existing account in the database.
- logout
  - Logs out of the currently logged account.
- disconnect
  - Disconnects current account from the server
- deposit-money <amount>
  - Deposits given amount of money in the wallet of the currently logged account.
- list-offerings
  - Lists every available for purchase cryptocurrency.
- buy <asset_code> <amount>
  - Purchases quantity of given cryptocurrency by given amount of money.
- sell <asset_code>
  - Sells cryptocurrency available in the wallet of the currently logged account.
- get-wallet-summary
  - Shows information about the wallet of the currently logged account, including current balance, which cryptocurrency they have in their wallet, as well as their quantity.
- get-wallet-overall-summary
  - Shows information about the overall winnings of the currently logged account.
- shutdown
  - Shuts the server down.
