#ifndef STD_LANG_ARRAY_P_H
#define STD_LANG_ARRAY_P_H
// source_filename = "std/lang/Array.p" ; 2019-03-10T15:07:05.863
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
// class structures
struct mtab_std_lang_Array {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
};
struct obj_std_lang_Array {
    struct mtab_std_lang_Array* _mtt;
    int32_t length; //1 public length
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_lang_Array* elements[0];
} array_obj_std_lang_Array;
// static fields
// static methods
extern array_int1_t* std_lang_Array_booleans_Pint(int32_t);
extern array_int8_t* std_lang_Array_bytes_Pint(int32_t);
extern array_int32_t* std_lang_Array_chars_Pint(int32_t);
extern array_double* std_lang_Array_doubles_Pint(int32_t);
extern array_float* std_lang_Array_floats_Pint(int32_t);
extern array_int32_t* std_lang_Array_ints_Pint(int32_t);
extern array_int64_t* std_lang_Array_longs_Pint(int32_t);
extern array_int16_t* std_lang_Array_shorts_Pint(int32_t);
extern array_obj_std_lang_Object* std_lang_Array_Objects_Pint(int32_t);
extern void std_lang_Array_copy_PObject_Y_Pint_Pint_PObject_Y_Pint(array_obj_std_lang_Object*,int32_t,int32_t,array_obj_std_lang_Object*,int32_t);
extern void std_lang_Array_copy_Pshort_Y_Pint_Pint_Pshort_Y_Pint(array_int16_t*,int32_t,int32_t,array_int16_t*,int32_t);
extern void std_lang_Array_copy_Plong_Y_Pint_Pint_Plong_Y_Pint(array_int64_t*,int32_t,int32_t,array_int64_t*,int32_t);
extern void std_lang_Array_copy_Pint_Y_Pint_Pint_Pint_Y_Pint(array_int32_t*,int32_t,int32_t,array_int32_t*,int32_t);
extern void std_lang_Array_copy_Pfloat_Y_Pint_Pint_Pfloat_Y_Pint(array_float*,int32_t,int32_t,array_float*,int32_t);
extern void std_lang_Array_copy_Pdouble_Y_Pint_Pint_Pdouble_Y_Pint(array_double*,int32_t,int32_t,array_double*,int32_t);
extern void std_lang_Array_copy_Pchar_Y_Pint_Pint_Pchar_Y_Pint(array_int32_t*,int32_t,int32_t,array_int32_t*,int32_t);
extern void std_lang_Array_copy_Pbyte_Y_Pint_Pint_Pbyte_Y_Pint(array_int8_t*,int32_t,int32_t,array_int8_t*,int32_t);
extern void std_lang_Array_copy_Pboolean_Y_Pint_Pint_Pboolean_Y_Pint(array_int1_t*,int32_t,int32_t,array_int1_t*,int32_t);
extern array_obj_std_lang_Object* std_lang_Array_copyOf_PObject_Y(array_obj_std_lang_Object*);
extern array_int16_t* std_lang_Array_copyOf_Pshort_Y(array_int16_t*);
extern array_int64_t* std_lang_Array_copyOf_Plong_Y(array_int64_t*);
extern array_int32_t* std_lang_Array_copyOf_Pint_Y(array_int32_t*);
extern array_float* std_lang_Array_copyOf_Pfloat_Y(array_float*);
extern array_double* std_lang_Array_copyOf_Pdouble_Y(array_double*);
extern array_int32_t* std_lang_Array_copyOf_Pchar_Y(array_int32_t*);
extern array_int8_t* std_lang_Array_copyOf_Pbyte_Y(array_int8_t*);
extern array_obj_std_lang_Object* std_lang_Array_copyOf_PObject_Y_Pint_Pint(array_obj_std_lang_Object*,int32_t,int32_t);
extern array_int16_t* std_lang_Array_copyOf_Pshort_Y_Pint_Pint(array_int16_t*,int32_t,int32_t);
extern array_int64_t* std_lang_Array_copyOf_Plong_Y_Pint_Pint(array_int64_t*,int32_t,int32_t);
extern array_int32_t* std_lang_Array_copyOf_Pint_Y_Pint_Pint(array_int32_t*,int32_t,int32_t);
extern array_float* std_lang_Array_copyOf_Pfloat_Y_Pint_Pint(array_float*,int32_t,int32_t);
extern array_double* std_lang_Array_copyOf_Pdouble_Y_Pint_Pint(array_double*,int32_t,int32_t);
extern array_int32_t* std_lang_Array_copyOf_Pchar_Y_Pint_Pint(array_int32_t*,int32_t,int32_t);
extern array_int8_t* std_lang_Array_copyOf_Pbyte_Y_Pint_Pint(array_int8_t*,int32_t,int32_t);
extern array_int1_t* std_lang_Array_copyOf_Pboolean_Y_Pint_Pint(array_int1_t*,int32_t,int32_t);
extern void std_lang_Array__class_initialize();
// referenced classes
#include "std/lang/String.h"
#include "std/lang/Throw.h"
#include "std/lang/Object.h"
#include "std/lang/Class.h"
#endif /* STD_LANG_ARRAY_P_H */
