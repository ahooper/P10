#ifndef STD_IO_WRITER_P_H
#define STD_IO_WRITER_P_H
// source_filename = "std/io/Writer.p" ; 2019-03-10T15:07:10.699
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_io_Writer obj_std_io_Writer;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
// class structures
struct mtab_std_io_Writer {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*close)(obj_std_io_Writer*); //2 public close[]
    int32_t (*write_Pbyte_Y_Pint_Pint)(obj_std_io_Writer*,array_int8_t*,int32_t,int32_t); //3 public write[byte[], int, int]
    int32_t (*write_Pbyte_Y)(obj_std_io_Writer*,array_int8_t*); //4 public write[byte[]]
    int32_t (*write_Pbyte_Y_Pint_Pint_Pbyte_Y_Pint_Pint)(obj_std_io_Writer*,array_int8_t*,int32_t,int32_t,array_int8_t*,int32_t,int32_t); //5 public write[byte[], int, int, byte[], int, int]
};
struct obj_std_io_Writer {
    struct mtab_std_io_Writer* _mtt;
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_io_Writer* elements[0];
} array_obj_std_io_Writer;
// static fields
// static methods
extern void std_io_Writer__class_initialize();
// referenced classes
#include "std/io/BufferedWriter.h"
#include "std/io/Reader.h"
#include "std/lang/System.h"
#include "std/lang/Object.h"
#include "std/lang/Throw.h"
#include "std/io/FileWriter.h"
#include "std/lang/Array.h"
#include "std/lang/String.h"
#include "std/lang/Class.h"
#endif /* STD_IO_WRITER_P_H */
