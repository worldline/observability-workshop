+++
date = '2025-06-06T23:15:06+02:00'
title = 'Validation'
weight = 4
+++

✅ Open the [Eureka](https://cloud.spring.io/spring-cloud-netflix/) website started during the infrastructure setup. The
following instances should be registered with Eureka:

* API-GATEWAY
* EASYPAY-SERVICE
* FRAUDETECT-SERVICE
* MERCHANT-BACKOFFICE
* SMARTBANK-GATEWAY

> [!TIP]
> If you run this workshop on your desktop, you can go to this URL: [http://localhost:8761](http://localhost:8761).    
> If you run it on a CDE, you can go to the corresponding URL instead by going into the `PORTS` view and
> select the url next to the port `8761`. You may have to `Add Port` manually if not detected by VSCode.

✅ All services should be registered before continuing…

🛠️ You can now access our platform to initiate a payment:

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=25000
```

✅ You should get the following content:

```bash
HTTP/1.1 201 Created
Content-Type: application/json
Date: Wed, 05 Jun 2024 13:42:12 GMT
Location: http://172.19.25.95:44523/payments/3cd8df14-8c39-460b-a429-dc113d003aed
transfer-encoding: chunked

{
    "amount": 25000,
    "authorId": "5d364f1a-569c-4c1d-9735-619947ccbea6",
    "authorized": true,
    "bankCalled": true,
    "cardNumber": "5555567898780008",
    "cardType": "MASTERCARD",
    "expiryDate": "789456123",
    "paymentId": "3cd8df14-8c39-460b-a429-dc113d003aed",
    "posId": "POS-01",
    "processingMode": "STANDARD",
    "responseCode": "ACCEPTED",
    "responseTime": 414
}
```