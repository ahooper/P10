#ifndef HELLO2_P_H
#define HELLO2_P_H
// source_filename = "Hello2.p" ; 2019-03-10T15:07:48.923
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_test_Hello2 obj_test_Hello2;
typedef struct obj_std_io_Writer obj_std_io_Writer;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_text_Formatter obj_std_text_Formatter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
// class structures
struct mtab_test_Hello2 {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*new)(obj_test_Hello2*); //2 public new[]
};
struct obj_test_Hello2 {
    struct mtab_test_Hello2* _mtt;
    int32_t id; //1 public id
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_test_Hello2* elements[0];
} array_obj_test_Hello2;
// static fields
extern int32_t test_Hello2_serial;
// static methods
extern obj_test_Hello2* test_Hello2___newInstance();
extern int32_t test_Hello2_main_PString_Y(array_obj_std_lang_String*);
extern void test_Hello2_breakpoint();
extern void test_Hello2__class_initialize();
// referenced classes
#include "std/io/FileWriter.h"
#include "std/lang/String.h"
#include "std/lang/System.h"
#include "std/io/BufferedWriter.h"
#include "std/io/Reader.h"
#include "std/lang/Object.h"
#include "std/lang/Array.h"
#include "std/io/Writer.h"
#include "std/lang/Class.h"
#include "std/lang/Throw.h"
#include "std/text/Formatter.h"
#endif /* HELLO2_P_H */
