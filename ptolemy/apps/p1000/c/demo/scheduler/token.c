#include "token.h"

void TOKEN_init(TOKEN* token, void* actual_ref)
{
	INIT_SUPER_TYPE(TOKEN, GENERAL_TYPE, token, actual_ref, NULL);
	
	token->prev = token->next = NULL;
}
