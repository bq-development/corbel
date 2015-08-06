package io.corbel.iam.auth.rule;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.exception.NoSuchPrincipalException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
public class PrincipalExistsAuthorizationRule implements AuthorizationRule {

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        if (context.hasPrincipal() && context.getPrincipal() == null) {
            throw new NoSuchPrincipalException(Message.PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(context.getPrincipalId(), context
                    .getIssuerClient().getDomain()));
        }
    }
}
