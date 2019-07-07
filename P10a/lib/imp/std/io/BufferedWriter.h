#ifndef STD_IO_BUFFEREDWRITER_P_H
#define STD_IO_BUFFEREDWRITER_P_H
// source_filename = "std/io/BufferedWriter.p" ; 2019-03-10T15:07:08.724
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Writer obj_std_io_Writer;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
// class structures
struct mtab_std_io_BufferedWriter {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*close)(obj_std_io_BufferedWriter*); //2 public close[]
    void (*write_Pbyte_Y_Pint_Pint)(obj_std_io_BufferedWriter*,array_int8_t*,int32_t,int32_t); //3 public write[byte[], int, int]
    int32_t (*write_Pbyte_Y)(obj_std_io_Writer*,array_int8_t*); //4 public write[byte[]]
    int32_t (*write_Pbyte_Y_Pint_Pint_Pbyte_Y_Pint_Pint)(obj_std_io_Writer*,array_int8_t*,int32_t,int32_t,array_int8_t*,int32_t,int32_t); //5 public write[byte[], int, int, byte[], int, int]
    void (*new_PWriter)(obj_std_io_BufferedWriter*,obj_std_io_Writer*); //6 public new[Writer]
    void (*new_PWriter_Pint)(obj_std_io_BufferedWriter*,obj_std_io_Writer*,int32_t); //7 public new[Writer, int]
    void (*flush)(obj_std_io_BufferedWriter*); //8 public flush[]
    void (*write_Pbyte)(obj_std_io_BufferedWriter*,int8_t); //9 public write[byte]
    void (*write_Pchar)(obj_std_io_BufferedWriter*,int32_t); //10 public write[char]
    void (*write_PString)(obj_std_io_BufferedWriter*,obj_std_lang_String*); //11 public write[String]
};
struct obj_std_io_BufferedWriter {
    struct mtab_std_io_BufferedWriter* _mtt;
    obj_std_io_Writer* w; //1 private w
    array_int8_t* buffer; //2 private buffer
    int32_t count; //3 private count
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_io_BufferedWriter* elements[0];
} array_obj_std_io_BufferedWriter;
// static fields
// static methods
extern obj_std_io_BufferedWriter* std_io_BufferedWriter___newInstance_PWriter_Pint(obj_std_io_Writer*,int32_t);
extern obj_std_io_BufferedWriter* std_io_BufferedWriter___newInstance_PWriter(obj_std_io_Writer*);
extern void std_io_BufferedWriter__class_initialize();
// referenced classes
#include "std/lang/String.h"
#include "std/lang/System.h"
#include "std/io/FileWriter.h"
#include "std/io/Writer.h"
#include "std/lang/Object.h"
#include "std/io/Reader.h"
#include "std/lang/Array.h"
#include "std/lang/Class.h"
#include "std/lang/Throw.h"
#endif /* STD_IO_BUFFEREDWRITER_P_H */
