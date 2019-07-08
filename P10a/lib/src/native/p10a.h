/*	p10a.h

	Copyright 2019 Andrew Hooper

	This file is part of the P10 Compiler.

	The P10 Compiler is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
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
