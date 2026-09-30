# Herramienta de carga de resultados

Programa de línea de comandos que sube los resultados de los sorteos a Firestore
(`sorteos/{id}`). Usa el Admin SDK de Firebase, así que no depende de las reglas de seguridad.

## Cómo se usa en el día a día

Pedirle a Claude, por ejemplo: *"cargá el Quini 6 del domingo"* o *"cargá la Nocturna de hoy"*.
Claude busca los números en al menos dos fuentes, muestra la vista previa y, si coinciden, los carga.

## Requisito: la clave de la cuenta de servicio

1. Firebase Console → ⚙ Configuración del proyecto → **Cuentas de servicio** → **Generar nueva clave privada**.
2. Guardar el archivo como `admin/credenciales.json` (está en `.gitignore`: **nunca se sube al repo**).

Esa clave da acceso total a la base: no compartirla ni pegarla en ningún lado.

## Comandos

Todos se corren desde la raíz del proyecto. **Sin `--confirmar` solo muestran la vista previa.**

```bash
# Quini 6: las cuatro extracciones y con cuántos aciertos pagó el Siempre Sale.
# El Pozo Extra se calcula solo.
./gradlew -q :admin:run --args="quini6 --fecha 2026-09-27 --sorteo 3412 \
  --tradicional 03,04,19,21,31,36 --segunda 03,08,11,12,14,21 --revancha 08,25,29,34,42,43 \
  --siempre-sale 01,07,11,31,32,45 --siempre-sale-aciertos 5 --confirmar"

# Quiniela: los 20 números en orden (posición 1 = la cabeza).
# Jurisdicciones: NACIONAL, BUENOS_AIRES, CORDOBA, SANTA_FE
# Turnos: PREVIA, PRIMERA, MATUTINA, VESPERTINA, NOCTURNA
./gradlew -q :admin:run --args="quiniela --fecha 2026-09-30 --jurisdiccion NACIONAL --turno NOCTURNA \
  --numeros 4523,0817,9910,... --confirmar"

# Ver lo que ya está cargado para una fecha
./gradlew -q :admin:run --args="ver --fecha 2026-09-30"
```

Si un sorteo ya estaba cargado con otros números, el comando se niega a pisarlo:
hay que revisarlo con `ver` y agregar `--reemplazar` si corresponde.

## Notificaciones

Al cargar un resultado con `--confirmar` se envía automáticamente el aviso a los usuarios
suscriptos (tema `quini6` o `quiniela-<JURISDICCION>`). Si se reemplaza un resultado, el aviso
dice que fue corregido. Para cargar sin avisar: `--sin-aviso`.

```bash
# Reenviar el aviso de un resultado ya cargado (sirve para probar)
./gradlew -q :admin:run --args="avisar --juego quini6 --fecha 2026-09-27"
./gradlew -q :admin:run --args="avisar --juego quiniela --fecha 2026-09-30 --jurisdiccion NACIONAL --turno PREVIA"
```
