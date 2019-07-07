#ifndef STD_IO_INTERIMIO_P_H
#define STD_IO_INTERIMIO_P_H
// source_filename = "std/io/InterimIO.p" ; 2019-03-10T15:07:09.259
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_io_InterimIO obj_std_io_InterimIO;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
typedef struct obj_std_io_Writer obj_std_io_Writer;
// class structures
struct mtab_std_io_InterimIO {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
};
struct obj_std_io_InterimIO {
    struct mtab_std_io_InterimIO* _mtt;
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_io_InterimIO* elements[0];
} array_obj_std_io_InterimIO;
// static fields
// static methods
extern int32_t std_io_InterimIO_putByte_Pint(int32_t);
extern int32_t std_io_InterimIO_putChar_Pint(int32_t);
extern void std_io_InterimIO_print_Pint(int32_t);
extern void std_io_InterimIO_print_Pbyte_Y(array_int8_t*);
extern void std_io_InterimIO_print_PString(obj_std_lang_String*);
extern void std_io_InterimIO__class_initialize();
// referenced classes
#include "std/lang/System.h"
#include "std/io/FileWriter.h"
#include "std/io/BufferedWriter.h"
#include "std/lang/Throw.h"
#include "std/lang/Object.h"
#include "std/io/Writer.h"
#include "std/lang/Class.h"
#include "std/io/Reader.h"
#include "std/lang/String.h"
#include "std/lang/Array.h"
#endif /* STD_IO_INTERIMIO_P_H */
