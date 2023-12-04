# README for Crypto-Fiat Tracker Project

## Introduction
The Crypto-Fiat Tracker is a Java-based application that allows users to track their cryptocurrency transactions and evaluate their portfolio in fiat currency terms. It provides insights into the historical performance of transactions and the current valuation of the cryptocurrency holdings.

## Running the Project

### Prerequisites
- Java Development Kit (JDK) 21 or higher

### Steps to Run
1. Clone the repository to your local machine.
2. Navigate to the project directory.
The project can be run using Gradle or Docker
#### Gradle
3. Run the following command to build the project:

`./gradlew build`

4. To start the application, run:

``gradle spring-boot:run`

Or if you want to define a custom port; 

`gradle spring-boot:run -Dspring-boot.run.arguments=--server.port=YOUR_DESIRED_PORT`

Replace `YOUR_DESIRED_PORT` with the port number you wish to use. 

#### Docker
To run a docker image that will be cleared after running call: 

`docker run --rm -it $(docker build -q .)`

## Project Functionality
- Allows users to input a Bitcoin address and view associated transactions. The transactions are enriched with the value at moment of buying and the current value. 
- Calculates and displays the Dollar Cost Averaging (DCA) profit for the given address.
- Provides data visualization for the cryptocurrency and fiat currency balance.

## Price Retrieval
Prices are retrieved using the `PricesService` class, which interacts with the CoinGecko API client implementation (`CoinGeckoApiClientImpl`). The service initializes a `ConcurrentSkipListMap` with price data and schedules regular updates to ensure the latest prices are available.

## Frontend Libraries
The project uses the following frontend libraries:
- **Chart.js**: For rendering interactive charts that display the user's transaction data over time.
- **DataTables**: To provide sortable and searchable tables that list the user's transactions.

## Limitations
- The project currently supports only Bitcoin transactions.
- Price data is limited to the BTC and EUR.
- The application does not support real-time transaction updates; it requires manual input to refresh data.

## External Services
- **Blockstream API**: Used to retrieve transaction data for a given Bitcoin address.
- **CoinGecko API**: Utilized to fetch current and historical cryptocurrency prices in various fiat currencies.
