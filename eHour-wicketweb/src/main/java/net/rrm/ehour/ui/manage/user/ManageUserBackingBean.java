/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.ui.manage.user;

import net.rrm.ehour.domain.User;
import net.rrm.ehour.ui.common.model.AdminBackingBeanImpl;
import net.rrm.ehour.user.service.LdapUser;

/**
 * Backing bean for users
 */

public class ManageUserBackingBean extends AdminBackingBeanImpl<User> {
    private static final long serialVersionUID = 2781902854421696575L;
    private final LdapUser user;
    private final boolean editMode;

    private boolean showAssignments;

    public ManageUserBackingBean(LdapUser user) {
        this(user, true);
    }


    private ManageUserBackingBean(LdapUser user, boolean editMode) {
        this.editMode = editMode;
        this.user = user;

    }

    public boolean isShowAssignments() {
        return showAssignments;
    }

    public void setShowAssignments(boolean showAssignments) {
        this.showAssignments = showAssignments;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public User getUser() {
        return user.getUser();
    }

    @Override
    public User getDomainObject() {
        return getUser();
    }
}
