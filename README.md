# matchstick
Toy program to generate matchstick diagrams of ordinals.

Matchstick diagrams are a way to visualize ordinals.
They are helpful in learning to understand the order types of some small ordinals.

![Matchstick diagram of ω^2 + ω][sample-00C000CC0CC]

After *ω³* or so, everything starts to look about the same, and it is difficult to tell at a glance what ordinal a matchstick diagram represents.

![Matchstick diagram of ω^4][sample-00000CCCC0C]

![Matchstick diagram of ω^(ω2)][sample-00C00C0CC0C]

![Matchstick diagram of ω^(ω+2)][sample-0000C0CCC0C]

![Matchstick diagram of ω^ω^ω^ω][sample-00C0C0C0C0C]

This was a one-off project, more comparable to a script than an app.
The code quality is not representative of what I usually make, and the code is not easily reusable for other purposes,
but I am publishing it anyway for educational purposes, in hopes that others may learn from it.
The only reason I chose Java was for a balance of ease of use and performance -
the rendering code is about as simple as it can get,
and Java is usually not too much slower at heavy computation work compared to the C/C++ equivalents.
If not for the performance concerns, I would have gone with Python as usual.

[sample-00C000CC0CC]: samples/00C000CC0CC
[sample-00000CCCC0C]: samples/00000CCCC0C
[sample-00C00C0CC0C]: samples/00C00C0CC0C
[sample-0000C0CCC0C]: samples/0000C0CCC0C
[sample-00C0C0C0C0C]: samples/00C0C0C0C0C
