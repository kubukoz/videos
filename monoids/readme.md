#### Monoids video notes

# `Semigroup[A]`

A binary operation:

```scala
def combine: (A, A) => A
```

## Laws

### Associativity

```scala
(x |+| y) |+| z <-> x |+| (y |+| z)
```

```scala
List(x,y,z,  a,b,c,  ...).combineAll

<->

List(x, y, z).combineAll |+|
List(a,b,c).combineAll |+|
List(...).combineAll
```

# `Monoid[A]`

`Semigroup[A]` plus neutral element that absorbs the operation:

```scala
def empty: A
```

## Laws

### Left identity

```scala
(empty |+| x) <-> x
```

### Right identity

```scala
(x |+| empty) <-> x
```
