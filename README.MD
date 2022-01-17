# common

## exception
- handler
  - `io.github.amrjlg.exception.handler.ExceptionHandler`
  - `io.github.amrjlg.exception.handler.AbstractExceptionHandler`
  - `io.github.amrjlg.exception.handler.DefaultExceptionHandler`
- exception
  - `io.github.amrjlg.exception.NoStackTraceException`

## id
id模块 id生成器

目前仅含有雪花算法

## interface
1. `io.github.amrjlg.api.Handler`
2. `io.github.amrjlg.api.Support`
3. `io.github.amrjlg.api.SupportHandler`通过`Support`接口判断是否执行`Handler`

## response
1. `io.github.amrjlg.responseResult<T>`基本定义
2. `io.github.amrjlg.responseSuccess<T>`默认成功响应
   1. `Success<T>#ok()`
   2. `Success<T>#ok(T)`
3. `io.github.amrjlg.responseFailure<T>`默认失败响应
   1. `Failure<T>#msg(String)`

## util
- `io.github.amrjlg.utils.ArrayUtil`
  - `array`构建基本类型数组
  - `arrays`构建对象数组
  - `comsumer`消费对象数组
  - `map`
  - `empty`
  - `emptyArray`
  - `swap` 交换数组指定索引的元素
  - `reserved`
- `io.github.amrjlg.utils.ByteUtil`
- `io.github.amrjlg.utils.CollectionUtil`
- ~~`io.github.amrjlg.utils.DateTimeUtil`~~ merge into date util at 1.5
- `io.github.amrjlg.utils.DateUtil`
- `io.github.amrjlg.utils.HexUtil`
- `io.github.amrjlg.utils.StringUtil`

## function
- `io.github.amrjlg.function`
  - 添加基本类型函数式接口
- `io.github.amrjlg.stream`
  1. 参考java 8 stream api，以方便实现其他基本类型的stream
  2. 将大多数内部类独立开来
  3. 拆分子包
     1. `node`
     2. `spliterator`
     3. `pipeline`
     4. `task`
     5. `sink`