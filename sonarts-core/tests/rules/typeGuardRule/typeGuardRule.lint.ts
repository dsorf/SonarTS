  function foo(p: MyType) {
//^^^^^^^^ {{Add type guard}}
  return (p as MySubType).prop != undefined;
}

function foo(p: MyType) p is MySubType {
  return (p as MySubType).prop != undefined;
}

function foo(p: MyType) {
  return (p as any).prop != undefined;
}

  function foo(p: MyType) {
//^^^^^^^^ {{Add type guard}}
    return !!(p as MySubType).prop;
  }
  
  function foo(p: MyType) {
//^^^^^^^^ {{Add type guard}}
    return !!((p as MySubType).prop);
  }
  


enum MyEnum {
  FOO, BAR
}

interface SuperInterface {
  prop: MyEnum
}

interface Foo extends SuperInterface {
  prop: MyEnum.FOO
}

interface Bar extends SuperInterface {
  prop: MyEnum.BAR
}

