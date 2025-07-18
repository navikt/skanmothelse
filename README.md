# Skanmothelse
Skanmothelse mottek og arkiverer papirsjukemeldingar den mottek frå Iron Mountain. 
Kvar sjukemelding vil resultere i éi pdf-fil, éi xml-fil og éi ocr-fil.

Du finn meir informasjon om  [skanmothelse på Confluence](https://confluence.adeo.no/display/BOA/skanmothelse).

Filene vi mottek frå Iron Mountain er kryptert med PGP-kryptering.

I fylgjande lenke finn du [oppskrift for skifte av pgp-nøkkelpar](https://confluence.adeo.no/display/BOA/PGP-kryptering+for+filer).

## Testing
### Oppskrift for testing av pgp-dekryptering i dev
To måtar som er nyttige, der dette er måte nr. 1:
* Få tilgang til mappa /inbound/skanmothelse på sftpserveren sftp-irmo-q.nav.no (devmiljøet).
* Flytt ei allereie eksisterande .zip.pgp-fil frå /inbound/skanmothelse/processed til /inbound/skanmothelse og vent til appen hentar fila (skjer kvart 5. minutt).

Måte nr. 2:
* Få tilgang til mappa /inbound/skanmothelse på sftpserveren sftp-irmo-q.nav.no (devmiljøet).
* Legg inn noverande dev-public key for appen i pgp-mappa i /resources som du finn i skanmothelse/keys i nais secret. Denne har namnet pgppublickey.
* I getPublicKey()-metoda i PGPManualTest-testklassa kan du vise til fila over.
* Krypter ei .zip-mappe i skanmothelse-prosjektet frå /resources-mappa ved å bruke generateEncryptedFile() i PGPManualTest-testklassa.
* Legg inn ei .zip.pgp-fil på /inbound/skanmothelse og vent til appen hentar fila (skjer kvart 5. minutt). Det kan vere praktisk å maile denne til seg sjølv dersom ein arbeider lokalt og skal hente den ut på utviklarimage.

## Førespurnadar
Spørsmål om koda eller prosjektet kan stillast
på [Slack-kanalen for \#Team Dokumentløysingar](https://nav-it.slack.com/archives/C6W9E5GPJ).
