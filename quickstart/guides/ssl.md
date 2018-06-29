---
title: SSL
caption: How to get a free certificate and use SSL with Ktor
category: quickstart
keywords: tls ssl https let's encrypt letsencrypt
---

![](/quickstart/guides/ssl/lets-encrypt.svg)

### Configuring an `A` register pointing to the machine

First of all, you have to configure your domain or subdomain to point to the IP of the machine that
you are going to use for the certificate. You have to put here the public IP of the machine.
If that machine is behind routers, you will need to configure the router to DMZ the machine with the host,
or to redirect at least the port 80 (HTTP) to that machine, and later you will probably want to configure the
port 443 (HTTPS) too.

Let's Encrypt always access the PORT 80 of your public IP, even if you configure Ktor to bind in other port,
you have to configure your routes to redirect the port 80 to the right local ip and port of the machine
hosting ktor.
{: .note }

### Generating a certificate

The Ktor server must not be running, and you have to execute the following command
(changing `my.example.com`, `root@example.com` and `8889`).

This command will start a HTTP web server in the specified port (that must be available as port 80 in the
public network, or you can forward ports in your router to 80:8889, and the domain must point to your public IP),
it will request a challenge, and will expose the `/.well-known/acme-challenge/file` with the proper content,
will generate a domain private key, and will retrieve the certificate chain:  

```
export DOMAIN=my.example.com
export EMAIL=root@example.com
export PORT=8889
export ALIAS=myalias
certbot certonly -n -d $DOMAIN --email "$EMAIL" --agree-tos --standalone --preferred-challenges http --http-01-port $PORT
```

<table>
<tr>
<td markdown="1" style="width:50%;">
❌ Error output sample:

```aidl
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator standalone, Installer None
Obtaining a new certificate
Performing the following challenges:
http-01 challenge for my.example.com
Waiting for verification...
Cleaning up challenges
Failed authorization procedure. my.example.com (http-01): urn:acme:error:connection :: The server could not connect to the client to verify the domain :: Fetching http://my.example.com/.well-known/acme-challenge/j-BJXA5ZGXdJuZhTByL4B95XBpiaGjZsm8JdCcA3Vr4: Timeout during connect (likely firewall problem)

IMPORTANT NOTES:
 - The following errors were reported by the server:

   Domain: my.example.com
   Type:   connection
   Detail: Fetching
   http://my.example.com/.well-known/acme-challenge/j-BJXA5ZGXdJuZhTByL4B9zXBp3aGjZsm8JdCcA3Vr4:
   Timeout during connect (likely firewall problem)

   To fix these errors, please make sure that your domain name was
   entered correctly and the DNS A/AAAA record(s) for that domain
   contain(s) the right IP address. Additionally, please check that
   your computer has a publicly routable IP address and that no
   firewalls are preventing the server from communicating with the
   client. If you're using the webroot plugin, you should also verify
   that you are serving files from the webroot path you provided.
 - Your account credentials have been saved in your Certbot
   configuration directory at /etc/letsencrypt. You should make a
   secure backup of this folder now. This configuration directory will
   also contain certificates and private keys obtained by Certbot so
   making regular backups of this folder is ideal.
```
</td>
<td markdown="1" style="width:50%;">
✅ Working output sample:

```aidl
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator standalone, Installer None
Obtaining a new certificate
Performing the following challenges:
http-01 challenge for my.example.com
Waiting for verification...
Cleaning up challenges

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /etc/letsencrypt/live/my.example.com/fullchain.pem
   Your key file has been saved at:
   /etc/letsencrypt/live/my.example.com/privkey.pem
   Your cert will expire on 2018-09-27. To obtain a new or tweaked
   version of this certificate in the future, simply run certbot
   again. To non-interactively renew *all* of your certificates, run
   "certbot renew"
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le
```
</td>
</tr>
</table>

### Converting the private key and certificate for Ktor

Now you have to convert the private key and certificates written by `certbot` to a format that Ktor understands.

The chain and private keys are stored in `/etc/letsencrypt/live/$DOMAIN` as `fullchain.pem` and `privkey.pem`.

```
openssl pkcs12 -export -out /etc/letsencrypt/live/$DOMAIN/keystore.p12 -inkey /etc/letsencrypt/live/$DOMAIN/privkey.pem -in /etc/letsencrypt/live/$DOMAIN/fullchain.pem -name $ALIAS
```

This will request a password for the export (you need to provide one for the next step to work):

```
Enter Export Password: mypassword
Verifying - Enter Export Password: mypassword
```

With th p12 file, we can use the `keytool` cli to generate a JKS file: 

```
keytool -importkeystore -alias $ALIAS -destkeystore /etc/letsencrypt/live/$DOMAIN/keystore.jks -srcstoretype PKCS12 -srckeystore /etc/letsencrypt/live/$DOMAIN/keystore.p12
```

### Configuring Ktor to use the generated JKS

Now you have to update your `application.conf` HOCON file, to configure the SSL port, the keyStore, alias and passwords.
You have to set the right values for your specific case: 

```groovy
ktor {
    deployment {
        port = 8889
        port = ${?PORT}
        sslPort = 8890
        sslPort = ${?PORT_SSL}
    }
    application {
        modules = [ com.example.ApplicationKt.module ]
    }
    security {
        ssl {
            keyStore = /etc/letsencrypt/live/mydomain.com/keystore.jks
            keyAlias = myalias
            keyStorePassword = mypassword
            privateKeyPassword = mypassword
        }
    }
}
```

If everything went well, Ktor should be listening at port 8889 in HTTP and listening at port 8890 in HTTPS. 