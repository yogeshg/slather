# slather

## links
 * [Problem Statement] (http://www.cs.columbia.edu/~kar/4444f16/node18.html)
 * [Files] (https://courseworks2.columbia.edu/courses/10838/files)

## commands
    javac slather/sim/Simulator.java
    javac slather/g2/Player.java
    java slather.sim.Simulator --gui --fps 10

## collaboration
Each member can publish to their own branch and merging can be done whenever required.

## Notes
 * Circle with circumference 2 x t
   - This strategy is good when playing 1 against many g0
   - Does not perform well with just g0 vs g2
   - Inflexion point around 40 points with 1 g2 vs 9 g0
   - [ ] TODO: Spawn "scouts" with random probability
   - If too many collisions, scout!

