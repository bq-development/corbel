package io.corbel.oauth.model;

/**
 * @author by Alberto J. Rubio
 */
public enum Role {

    USER {

        @Override
        public boolean canUpdate(Role role) {
            return false;
        }

        @Override
        public boolean canChangeRoleTo(Role role) {
            return role == USER;
        }
    },

    ADMIN {

        @Override
        public boolean canUpdate(Role role) {
            return role == USER;
        }

        @Override
        public boolean canChangeRoleTo(Role role) {
            return role != ROOT;
        }
    },

    ROOT {

        @Override
        public boolean canUpdate(Role role) {
            return role != ROOT;
        }

        @Override
        public boolean canChangeRoleTo(Role role) {
            return true;
        }
    };

    public abstract boolean canUpdate(Role role);

    public abstract boolean canChangeRoleTo(Role role);
}

