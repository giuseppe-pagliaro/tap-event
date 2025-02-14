# Tap Event

## User Default

- Giuseppe Pagliaro, pass (owner in un evento, guest nell'altro)
- Giovanni Verdi, pass (organizer in un evento, owner nell'altro)
- Malcom Smith, pass (organizer in tutti e due gli eventi)
- Alberto Toscano, pass (standkeeper in tutti e due gli eventi)
- KIOSK, pass (guest in tutti gli eventi)

## Bug Noti

- Race condition sul Callback della pre-popolazione del database, che potrebbe portare la prima query (solitamente il login) ad essere eseguita prima che i dati siano presenti. Dalla seconda query in poi funziona.
- I live data dei campi "Sold in" vengono rimossi prima del dovuto.
