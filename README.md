# Inferno Stats

A plugin to give you in-depth stats about your inferno attempt.

## Configuration

### Effective Restoration

* Default: **On**
* Description: Calculates same-tick health and prayer loss into special attack. For example, you are 50/99 health and 50/99 prayer
wielding an SGS. You use the special attack on a bat and hit a 20, killing it. This would regain 10 health and 5 prayer,
but in the process you were hit by the bat for an 18 and using the piety prayer which drained 1 prayer point.
The resultant restoration would effectively be -8 health and 4 prayer.
* Notes: Disabling *effective restoration* takes the special attack hits seen from enemies and calculates the appropriate health
or prayer restoration. There is a known issue regarding successful hits here.


### Wave Times

* Default: **Off**
* Displays a message similar to "Wave Completed in: 00:12" after every wave completion.

### Split Times

* Default: **On**
* Description: Displays a message similar to "Wave Split: 00:12" at the start of every wave split.
* Notes: Wave splits are 9, 18, 25, 35, 42, 50, 57, 60, 63, 66, 67, 68, and 69.

### Wave Timer

* Default: **Off**
* Description: Displays an infobox of the plugin's internal wave timer. This should be similar to the default Runelite one.

### Special Counter

* Default: **On**
* Description: Displays an infobox of the regenerative effects of special attacks used within the inferno.
* Notes: Viable weapons include: Saradomin Godsword, Saradomin Godsword (or), Toxic Blowpipe, Eldritch Staff

### Hide

* Default: **On**
* Description: Hides the sidebar button when the user is not in the inferno.

## Known Issues

* *Effective restoration*: There is no feasible way to know whether the SGS hit a successful 0 or not.