#include <stdint.h>
typedef int8_t int1_t; // TODO
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	int1_t elements[0];
} array_int1_t;
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	int8_t elements[0];
} array_int8_t;
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	int16_t elements[0];
} array_int16_t;
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	int32_t elements[0];
} array_int32_t;
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	int64_t elements[0];
} array_int64_t;
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	float elements[0];
} array_float;
typedef struct {
	struct mtab_std_lang_Array* _mtt;
	int32_t length;
	double elements[0];
} array_double;
#include "std/lang/Object.h"
