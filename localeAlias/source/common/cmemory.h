/*
******************************************************************************
*
*   Copyright (C) 1997-2008, International Business Machines
*   Corporation and others.  All Rights Reserved.
*
******************************************************************************
*
* File CMEMORY.H
*
*  Contains stdlib.h/string.h memory functions
*
* @author       Bertrand A. Damiba
*
* Modification History:
*
*   Date        Name        Description
*   6/20/98     Bertrand    Created.
*  05/03/99     stephen     Changed from functions to macros.
*
******************************************************************************
*/

#ifndef CMEMORY_H
#define CMEMORY_H

#include "unicode/utypes.h"
#include <stddef.h>
#include <string.h>


#define uprv_memcpy(dst, src, size) U_STANDARD_CPP_NAMESPACE memcpy(dst, src, size)
#define uprv_memmove(dst, src, size) U_STANDARD_CPP_NAMESPACE memmove(dst, src, size)
#define uprv_memset(buffer, mark, size) U_STANDARD_CPP_NAMESPACE memset(buffer, mark, size)
#define uprv_memcmp(buffer1, buffer2, size) U_STANDARD_CPP_NAMESPACE memcmp(buffer1, buffer2,size)

U_CAPI void * U_EXPORT2
uprv_malloc(size_t s);

U_CAPI void * U_EXPORT2
uprv_realloc(void *mem, size_t size);

U_CAPI void U_EXPORT2
uprv_free(void *mem);

/**
 * This should align the memory properly on any machine.
 * This is very useful for the safeClone functions.
 */
typedef union {
    long    t1;
    double  t2;
    void   *t3;
} UAlignedMemory;

/**
 * Get the least significant bits of a pointer (a memory address).
 * For example, with a mask of 3, the macro gets the 2 least significant bits,
 * which will be 0 if the pointer is 32-bit (4-byte) aligned.
 *
 * ptrdiff_t is the most appropriate integer type to cast to.
 * size_t should work too, since on most (or all?) platforms it has the same
 * width as ptrdiff_t.
 */
#define U_POINTER_MASK_LSB(ptr, mask) (((ptrdiff_t)(char *)(ptr)) & (mask))

/**
 * Get the amount of bytes that a pointer is off by from
 * the previous UAlignedMemory-aligned pointer.
 */
#define U_ALIGNMENT_OFFSET(ptr) U_POINTER_MASK_LSB(ptr, sizeof(UAlignedMemory) - 1)

/**
 * Get the amount of bytes to add to a pointer
 * in order to get the next UAlignedMemory-aligned address.
 */
#define U_ALIGNMENT_OFFSET_UP(ptr) (sizeof(UAlignedMemory) - U_ALIGNMENT_OFFSET(ptr))

/**
  *  Indicate whether the ICU allocation functions have been used.
  *  This is used to determine whether ICU is in an initial, unused state.
  */
U_CFUNC UBool 
cmemory_inUse(void);

/**
  *  Heap clean up function, called from u_cleanup()
  *    Clears any user heap functions from u_setMemoryFunctions()
  *    Does NOT deallocate any remaining allocated memory.
  */
U_CFUNC UBool 
cmemory_cleanup(void);

#endif
