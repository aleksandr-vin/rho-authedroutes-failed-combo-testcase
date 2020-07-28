# Combining authenticated and non-authenticated routes in Http4s + Rho

Given two routes:

1. `/joke`
2. `/hello/{name}`

We want to permit only authenticated requests to `/hello/{name}` and
keep `/joke` non-authenticated.

It is possible because we combine them so non-authenticated goes first
and it rejects `/hello` path of the request, allowing the
authenticated routes to process it.

If we want to make only a different method (for ex. _PUT_) to
`/hello/{name}` authenticated, then it gets tricky.

So we have:

1. GET `/joke`
2. GET `/hello/{name}`
3. PUT `/hello/{name}`

Let's try to make only #3 authenticated.

You'll get _405 Method Not Allowed_ response with reply from Http4s:
_PUT not allowed. Defined methods: GET_.
