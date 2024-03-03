package dmitryv.lab1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUsers {

    @XmlElement(name = "user")
    private List<XmlUser> users = null;


    public List<XmlUser> getUsers() {
        return users;
    }

    public void setUsers(List<XmlUser> users) {
        this.users = users;
    }
}