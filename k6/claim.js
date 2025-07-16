import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 500, // virtual users
  duration: '5s', // test duration
  thresholds: {
    http_req_failed: ['rate<0.01'],  // <1% errors
    http_req_duration: ['p(95)<1000'],  // 95% of requests under 1000ms
  },
};

export default function () {
  const couponId = 1;
  const payload = JSON.stringify({
    userId: Math.floor(Math.random() * 1000000),
    couponId: couponId,
  });

  const headers = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post('http://host.docker.internal:8080/api/coupons/issue', payload, headers);

}
