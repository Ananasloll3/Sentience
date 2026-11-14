# Debug and test tools

## `/sentience debug`

Toggles the debug display (operators):

- **above each agent**: alert level, the sense behind the focus (👂 hearing, 👁 sight, 👃 smell,
  ⚔ pain, ✋ touch, 📣 radio), score, uncertainty (±), sight meter, level of detail;
- **particles** where the agent **believes** it heard something (colour by noise type);
- **scent trails** around you: green = trace, red = blood, grey = in water;
- **your microphone level** in the action bar (with Simple Voice Chat).

```
HUNT  👂 22.2 ±2.9  👁40%
```

Mobs' real names are restored when leaving debug, and a debug name accidentally saved
with the world disappears on reload.

## `/sentience bruit <dB>`

Emits a test noise at your position.

## Demo mod tools

| Command | Effect |
|---|---|
| `/sentiencetest marcheur <pos> <seconds> [sneaking]` | a fake player walks back and forth (makes noise and leaves a scent) |
| `/sentiencetest piste <from> <to>` | lays a scent trail as if someone had just walked from A to B |
| `/sentiencetest arene <name>` | builds or resets a test arena in front of you |

!!! note "Fake player limitation"
    The walker doesn't really exist in the world: it makes noise and leaves a scent,
    but **sight** can't see it. Test sight on yourself or on villagers.
