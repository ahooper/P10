#ifndef HELLO1_P_H
#define HELLO1_P_H
// source_filename = "Hello1.p" ; 2019-02-23T16:19:11.824
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_test_Hello1 obj_test_Hello1;
typedef struct obj_std_io_InterimIO obj_std_io_InterimIO;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
typedef struct obj_std_io_Writer obj_std_io_Writer;
// class structures
struct mtab_test_Hello1 {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
};
struct obj_test_Hello1 {
    struct mtab_test_Hello1* _mtt;
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_test_Hello1* elements[0];
} array_obj_test_Hello1;
// static fields
// static methods
extern int32_t test_Hello1_main_PString_Y(array_obj_std_lang_String*);
extern void test_Hello1__class_initialize();
// referenced classes
#include "std/io/BufferedWriter.h"
#include "std/io/Writer.h"
#include "std/io/Reader.h"
#include "std/lang/Class.h"
#include "std/lang/Throw.h"
#include "std/lang/Array.h"
#include "std/io/FileWriter.h"
#include "std/io/InterimIO.h"
#include "std/lang/System.h"
#include "std/lang/Object.h"
#include "std/lang/String.h"
#endif /* HELLO1_P_H */
