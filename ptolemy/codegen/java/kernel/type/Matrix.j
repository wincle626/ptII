/***declareBlock***/
// java/kernel/type/Matrix.j
private class matrix {
    public int row;            // number of rows.
    public int column;         // number of columns.
    public Token []elements;            // matrix of pointers to the elements.
    //unsigned char elementsType;  // type of all the elements.
};

//typedef struct matrix* MatrixToken;
/**/


/***funcDeclareBlock***/

Token Matrix_get(Token token, int row, int column) {
    //return token.payload.Matrix->elements[column * token.payload.Matrix->row + row];
    return ((matrix)(token.payload)).elements[column * ((matrix)(token.payload)).row + row];
}

void Matrix_set(Token matrix, int row, int column, Token element) {
    //matrix.payload.Matrix->elements[column * matrix.payload.Matrix->row + row] = element;
    ((matrix)(matrix.payload)).elements[column * ((matrix)(matrix.payload)).row + row] = element;
}
/**/


/***Matrix_delete***/
Token Matrix_delete(Token token, Token... tokens) {
    int i, j;
    Token element, emptyToken;

    // Delete each elements.
    for (i = 0; i < ((matrix)(token.payload)).column; i++) {
	for (j = 0; j < ((matrix)(token.payload)).row; j++) {
            element = Matrix_get(token, j, i);
	    System.out.println("Matrix_delete: convert needs work");      
            //functionTable[(int) element.type][FUNC_delete](element);
        }
    }
    //free(token.payload.Matrix.elements);
    //free(token.payload.Matrix);

    return null;
}
/**/

/***Matrix_convert***/
Token Matrix_convert(Token token, Token... tokens) {
    /* token.payload.Matrix = (MatrixToken) malloc(sizeof(struct matrix));
       token.payload.Matrix->row = 1;
       token.payload.Matrix->column = 1;
       result.payload.Matrix->elements = (Token*) calloc(1, sizeof(Token));
       token.type = TYPE_Matrix;
       Matrix_set(token, 0, 0, token);
       return token;
    */
    return Matrix_new(1, 1, 1, token, token.type);
}
/**/


/***Matrix_new***/
// Create a new matrix from the given values.
// The "row" and "column" arguments specify the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token.
// The last element is the type, which is why this takes Object...
// and not Token...
Token Matrix_new(int row, int column, int given, Object... elements) {
    int i;
    Token result = new Token();
    int elementType;

    matrix matrix = new matrix();
    matrix.row = row;
    matrix.column = column;
    matrix.elements = new Token[row * column];

    result.type = TYPE_Matrix;
    result.payload = matrix;

    // Allocate a new matrix of Tokens.
    if (row > 0 && column > 0 && given > 0) {
        // Allocate an new 2-D array of Tokens.
        //result.payload.Matrix->elements = (Token*) calloc(row * column, sizeof(Token));
        //((matrix)(result.payload).elements = new Token[row * column];

	for (i = 0; i < given; i++) {
	    matrix.elements[i] = (Token)elements[i];
	}
	// elementType is given as the last argument.
	elementType = (Short)elements[i];

	if (elementType >= 0) {
	    // convert the elements if needed.
	    for (int j = 0; j < given; j++) {
		if (Matrix_get(result, j, 0).type != elementType) {
                        //result.payload.Matrix->elements[j] = functionTable[(int)elementType][FUNC_convert](Matrix_get(result, j, 0));
		switch(elementType) {
		    case TYPE_Array:
		        System.out.println("Matrix_ on an array of arrays, possible problem");
		        break;
		    case TYPE_Token:
		        Matrix_set(result, j, 0, Matrix_get(result, j, 0));
		        break;
#ifdef PTCG_TYPE_Double
		    case TYPE_Double:
		        Matrix_set(result, j, 0, Double_convert(Matrix_get(result, j, 0)));
		        break;
#endif
#ifdef PTCG_TYPE_Integer
		    case TYPE_Integer:
		        Matrix_set(result, j, 0, Integer_convert(Matrix_get(result, j, 0)));
		        break;
#endif
		    default:
		        throw new RuntimeException("Matrix_new(): Conversion from an unsupported type: "
						   +  elementType);
                   }
                }
            }
        }
    }
    return result;
}
/**/

/***Matrix_equals***/
boolean Matrix_equals(Token thisToken, Token... tokens) {
    int i, j;
    Token otherToken = tokens[0];

    if (((matrix)(thisToken.payload)).row != ((matrix)(otherToken.payload)).row 
	|| ((matrix)(thisToken.payload)).column != ((matrix)(otherToken.payload)).column) {
        return false;
    }
    for (i = 0; i < ((matrix)(thisToken.payload)).row; i++) {
	for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
            //if (!functionTable[(int) Matrix_get(thisToken, j, i).type][FUNC_equals](Matrix_get(thisToken, j, i), Matrix_get(otherToken, j, i)).payload.Boolean) {
	    if (!equals_Token_Token(Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j))) {
                return false;
            }
        }
    }
    return true;
}
/**/


/***Matrix_isCloseTo***/
Token Matrix_isCloseTo(Token thisToken, Token... elements) {
    int i, j;
    Token otherToken = elements[0];
    Token tolerance = elements[1];

    if (((matrix)(thisToken.payload)).row != ((matrix)(otherToken.payload)).row 
	|| ((matrix)(thisToken.payload)).column != ((matrix)(otherToken.payload)).column) {
        return Boolean_new(false);
    }
    for (i = 0; i < ((matrix)(thisToken.payload)).row; i++) {
	for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
	    if (!isCloseTo_Token_Token(Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j), tolerance)) {
                return Boolean_new(false);
            }
        }
    }
    return Boolean_new(true);
}
/**/

/***Matrix_print***/
Token Matrix_print(Token thisToken, Token... tokens) {
    // Token string = Matrix_toString(thisToken);
    // printf(string.payload.String);
    // free(string.payload.String);

    int i, j;
    printf("[");
    for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
        if (i != 0) {
            printf(", ");
        }
        for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
            if (j != 0) {
                printf("; ");
            }
            functionTable[((matrix)(thisToken.payload)).elements[i * ((matrix)(thisToken.payload)).row + j].type][FUNC_print](((matrix)(thisToken.payload)).elements[i]);
        }
    }
    printf("]");
}
/**/


/***Matrix_toString***/
Token Matrix_toString(Token thisToken, Token... tokens) {
    int i, j;
    int currentSize, allocatedSize;
    char* string;
    Token elementString;

    allocatedSize = 512;
    string = (char*) malloc(allocatedSize);
    string[0] = '[';
    string[1] = '\0';
    currentSize = 2;
    for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
        if (i != 0) {
            strcat(string, "; ");
        }
        for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
            if (j != 0) {
                strcat(string, ", ");
            }
            elementString = functionTable[(int) Matrix_get(thisToken, j, i).type][FUNC_toString](Matrix_get(thisToken, j, i));
            currentSize += strlen(elementString.payload.String);
            if (currentSize > allocatedSize) {
                allocatedSize *= 2;
                string = (char*) realloc(string, allocatedSize);
            }

            strcat(string, elementString.payload.String);
            free(elementString.payload.String);
        }
    }
    strcat(string, "]");
    return String_new(string);
}
/**/



/***Matrix_add***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Matrix_add(Token thisToken, Token... tokens) {
    int i, j;
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    result = Matrix_new(((matrix)(thisToken.payload)).row, ((matrix)(thisToken.payload)).column, 0);

    for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
        for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
            Matrix_set(result, j, i, functionTable[(int)Matrix_get(thisToken, i, j).type][FUNC_add](Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***Matrix_subtract***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Matrix_subtract(Token thisToken, Token... tokens) {
    int i, j;
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    result = Matrix_new(((matrix)(thisToken.payload)).row, ((matrix)(thisToken.payload)).column, 0);

    for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
        for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
            Matrix_set(result, j, i, functionTable[(int)Matrix_get(thisToken, i, j).type][FUNC_subtract](Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j)));
        }
    }

    va_end(argp);
    return result;
}
/**/




/***Matrix_multiply***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Matrix_multiply(Token thisToken, Token... tokens) {
    int i, j;
    va_list argp;
    Token result;
    Token element, otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    if (otherToken.type == TYPE_Matrix
	&& ((matrix)(otherToken.payload)).row == 1
            && ((matrix)(otherToken.payload)).column == 1) {
        // Handle simple scaling by a 1x1 matrix
        result = Matrix_new(((matrix)(thisToken.payload)).row, ((matrix)(thisToken.payload)).column, 0);
    } else {
        result = Matrix_new(((matrix)(thisToken.payload)).row, ((matrix)(thisToken.payload)).row, 0);
    }
    switch (otherToken.type) {
        case TYPE_Matrix:
        for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
            for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
                element = Matrix_get(thisToken, j, i);
                if (((matrix)(otherToken.payload)).row == 1
                        && ((matrix)(otherToken.payload)).column == 1) {
                    Matrix_set(result, j, i, functionTable[(int)element.type][FUNC_multiply](element, Matrix_get(otherToken, 0, 0)));
                }
            }
        }
        break;
        #ifdef TYPE_Array
        case TYPE_Array:
        element = Array_new(((matrix)(thisToken.payload)).column *
        ((matrix)(thisToken.payload)).row, 0);
        for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
            for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
                Array_set(element,
                i + ((matrix)(thisToken.payload)).row * j,
                Matrix_get(thisToken, j, i));
            }
        }
        break;
        #endif
        default:
        for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
            for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
                element = Matrix_get(thisToken, j, i);
                result.payload.Matrix.elements[i] = functionTable[(int)element.type][FUNC_multiply](element, otherToken);
            }
        }
    }
    va_end(argp);
    return result;
}
/**/

/***Matrix_divide***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Matrix_divide(Token thisToken, Token... tokens) {
    int i, j, index;
    va_list argp;
    Token result;
    Token element, otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    switch (otherToken.type) {
        case TYPE_Matrix:
        for (i = 0; i < ((matrix)(thisToken.payload)).column; i++) {
            for (j = 0; j < ((matrix)(thisToken.payload)).row; j++) {
                element = Matrix_get(thisToken, j, i);
                // FIXME: Need to program this.
            }
        }
        break;
        #ifdef TYPE_Array
        case TYPE_Array:
        // Divide reverse.
        result = Array_new(otherToken.payload.Array.size, 0);
        for (i = 0; i < otherToken.payload.Array.size; i++) {
            element = Array_get(thisToken, i);
            result.payload.Array.elements[i] = functionTable[TYPE_Matrix][FUNC_divide](thisToken, element);
        }

        break;
        #endif
        default:
        result = Matrix_new(((matrix)(thisToken.payload)).row, ((matrix)(thisToken.payload)).column, 0);

        for (i = 0, index = 0; i < ((matrix)(thisToken.payload)).column; i++) {
            for (j = 0; j < ((matrix)(thisToken.payload)).row; j++, index++) {
                element = Matrix_get(thisToken, j, i);
                result.payload.Matrix.elements[index] = functionTable[(int)element.type][FUNC_divide](element, otherToken);
            }
        }
    }
    va_end(argp);
    return result;
}
/**/

/***Matrix_toExpression***/
Token Matrix_toExpression(Token thisToken, Token... tokens) {
    return Matrix_toString(thisToken);
}
/**/

