+++
date = '2025-06-05T22:52:13+02:00'
draft = false
title = 'Our Application: EasyPay'
description = "Describes EasyPay architecture and components."
weight = 1
+++

## High Level Design

![The Easy Pay System](architecture.svg)

### API Gateway

Centralises all API calls.

### Easy Pay Service

Payment microservices which accepts (or not) payments.

This is how it validates every payment:

1. Check the POS (Point of Sell) number
2. Check the credit card number
3. Check the credit card type
4. Check the payment threshold, it calls the Smart Bank Gateway for authorization

If the payment is validated, it stores it and broadcasts it to all the other microservices through Kafka.

### Fraud detection Service

After fetching a message from the Kafka topic, this service search in its database if the payment's card number is
registered for fraud.

In this case, only a ``WARN`` log is thrown.

### Merchant Back Office Service

For this lab, it only simulates the subscription of messages.

### Smart Bank Gateway

This external service authorizes the payment.