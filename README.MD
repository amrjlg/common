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
1. `io.github.amrjlg.apiHandler`
2. `io.github.amrjlg.apiSupport`
3. `io.github.amrjlg.apiSupportHandler`通过`Support`接口判断是否执行`Handler`

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
- `io.github.amrjlg.utils.ByteUtil`
- `io.github.amrjlg.utils.CollectionUtil`
- `io.github.amrjlg.utils.DateTimeUtil`
- `io.github.amrjlg.utils.DateUtil`
- `io.github.amrjlg.utils.HexUtil`
- `io.github.amrjlg.utils.StringUtil`
