import http from 'k6/http';
import { sleep } from 'k6';
import { check } from 'k6';
import { randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const cardNumbers = [
    '5204460010000007',
    '5204460010000015',
    '5204460010000023',
    '5204460010000031',
    '5204460010000049',
    '5204460010000056',
    '5204460010000057'
]
const posIds = ["POS-01", "POS-02"]
const url = 'http://localhost:8080/api/easypay/payments'
const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': '*/*',
    },
  };

export default function()  {
    const randomCardNumber = randomItem(cardNumbers);
    const randomPosId = randomItem(posIds);

    const payload = JSON.stringify({
        'posId': randomPosId,
        'cardNumber': randomCardNumber,
        'expiryDate': '09/25',
        'amount': "40000"
    })

    const res = http.post(url, payload, params);

    check(res, {

        'is status 201': (r) => r.status === 201,

    });
}