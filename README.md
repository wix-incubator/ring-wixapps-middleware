# `ring-wixapps-middleware`

[Ring](http://github.com/mmcgrana/ring) middleware that checks the HMAC signature and passes the SignedInstance as a hash-map in the "instance" parameter to the controller.
If the signature is not valid, returns 403 to the client without calling the controller.

### Usage

#### With Noir - in server.clj
      (server/add-middleware wrap-wixapps-middleware {:algorithm "HmacSHA256" :secret-key "FIXME put key here"})

## License

Copyright (C) 2012 Dimitri Krassovski

Based on the HMAC check code by Dave Barker
Copyright (C) 2011 Dave Barker


Code distributed under the Eclipse Public License, the same as Clojure.

