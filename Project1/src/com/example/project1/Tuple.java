package com.example.project1;

public class Tuple<X, Y, Z> { 
  public final X first; 
  public Y second; 
  public final Z third;
  public Tuple(X x, Y y, Z z) { 
    this.first = x; 
    this.second = y; 
    this.third = z;
  } 
} 