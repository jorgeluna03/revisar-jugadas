# Revisar Jugadas — Plan del proyecto

App Android (Play Store) para anotar jugadas de **Quini 6** y **Quiniela**, ver los resultados
y controlar automáticamente los aciertos, con notificación cuando se publican los números.

> App **no oficial**. No usar nombres ni logos de las loterías. Mostrar siempre el aviso:
> "Verificá tu boleta en una agencia oficial".

---

## Decisiones tomadas

| Tema | Decisión |
|---|---|
| Plataforma | Android (Play Store) |
| Lenguaje app | Kotlin + Jetpack Compose (Android Studio) |
| Backend | Firebase: Authentication, Cloud Firestore, Cloud Functions (TypeScript), Cloud Messaging |
| Login | Google Sign-In + modo invitado (anónimo, vinculable luego a Google). Facebook: no por ahora |
| Carga de resultados | **Manual** desde una sección de administrador dentro de la app (solo visible para admins) |
| Juegos | Quini 6 y Quiniela |
| Quinielas | Nacional (Ciudad), Buenos Aires (Provincia), Córdoba, Santa Fe, Santiago del Estero |
| Package name | `com.jluna.revisarjugadas` (definitivo: no se puede cambiar una vez publicado) |

---

## Etapas

### Etapa 0 — Entorno
- [x] Instalar Android Studio (incluye SDK y emulador)
- [ ] Actualizar Node.js a LTS (22.x) — hoy está la 12
- [ ] Instalar Firebase CLI (`npm install -g firebase-tools`)
- [ ] Crear proyecto en Firebase Console ("revisar-jugadas")
- [ ] Registrar la app Android en Firebase y descargar `google-services.json`
- [x] Crear el proyecto Android base (compila OK) — falta abrirlo en Android Studio
- [ ] Cuenta de Google Play Console (USD 25) — puede esperar hasta la Etapa 3

### Etapa 1 — Base + Quini 6
- [x] Estructura de la app: navegación, tema, pantallas vacías
- [ ] Login con Google + modo invitado
- [ ] Cargar / editar / borrar jugadas de Quini 6
- [ ] Ver resultados del último sorteo y anteriores
- [ ] Sección admin: cargar resultados de Quini 6 (todas las modalidades) y publicar
- [ ] Cloud Function: al publicar, controlar las jugadas del sorteo y guardar aciertos
- [ ] Notificaciones: aviso general + aviso personal con el resultado de tus jugadas
- [ ] Reglas de seguridad de Firestore (cada usuario solo ve sus jugadas; solo admin escribe sorteos)

### Etapa 2 — Quiniela
- [ ] Cargar apuestas de Quiniela (1 a 4 cifras, posición, jurisdicción, turno)
- [ ] Sección admin: pegar texto con los 20 números → detectar, validar, publicar
- [ ] Control de aciertos + cálculo estimado de premio según tabla de pagos
- [ ] Preferencias de notificaciones (por juego / jurisdicción / turno)
- [ ] Redoblona (evaluar si entra en esta etapa o más adelante)

### Etapa 3 — Publicación
- [ ] Logo e ícono
- [ ] Política de privacidad (página web pública)
- [ ] Opción "Borrar mi cuenta" dentro de la app (requisito de Google)
- [ ] Formulario de Seguridad de los datos en Play Console
- [ ] Prueba cerrada: **12 testers durante 14 días** (requisito para cuentas personales nuevas)
- [ ] Publicación en producción

### Etapa 4 — Extras (futuro)
- [ ] Scrapers automáticos de resultados (con confirmación del admin)
- [ ] Escanear la boleta con la cámara (ML Kit)
- [ ] Jugadas fijas que se repiten cada sorteo
- [ ] Estadísticas (números más salidos, etc.)
- [ ] Más jurisdicciones (Entre Ríos, Mendoza, ...)

---

## Modelo de datos (Firestore, borrador)

```
usuarios/{uid}
  nombre, email, esAdmin, preferenciasNotif, tokensFCM[]

usuarios/{uid}/jugadas/{jugadaId}
  juego: "QUINI6" | "QUINIELA"
  sorteoId                  // referencia al sorteo
  // Quini 6
  numeros: [6 enteros 0..45], revancha: bool, siempreSale: bool
  // Quiniela
  numero: "1234" (1 a 4 cifras), posicion: 1 | 5 | 10 | 20, importe
  resultado: { controlada: bool, aciertos..., premioEstimado }

sorteos/{sorteoId}
  juego, fecha, estado: "PENDIENTE" | "PUBLICADO"
  // Quini 6
  numeroSorteo, tradicional[6], segunda[6], revancha[6], siempreSale[6], pozoExtra[]
  // Quiniela
  jurisdiccion, turno, numeros[20] (strings de 4 cifras, en orden de posición)
```

Ids legibles para sorteos: `QUINI6-3413`, `QUINIELA-NACIONAL-2026-09-29-NOCTURNA`.

---

## Reglas de juego (a verificar antes de implementar)

**Quini 6** — números del 00 al 45, sorteos miércoles y domingo.
- Tradicional y La Segunda: premian 6, 5 y 4 aciertos.
- Revancha: premia 6 aciertos (opcional en la boleta).
- Siempre Sale: premia a quienes tengan la mayor cantidad de aciertos (opcional).
- Pozo Extra: 6 aciertos entre los números de Tradicional + La Segunda + Revancha.

**Quiniela** — 20 números de 4 cifras por sorteo, lunes a sábado.
- Turnos: La Previa, La Primera, Matutina, Vespertina, Nocturna (confirmar cuáles tiene cada jurisdicción).
- Pago orientativo a la cabeza: 1 cifra ×7, 2 cifras ×70, 3 cifras ×600, 4 cifras ×3500.
- A los 5 / 10 / 20: el pago se divide por 5 / 10 / 20.
- Confirmar diferencias por jurisdicción.

---

## Pendientes / ideas
- Logo: más adelante.
