package dmitryv.lab1.services;

import dmitryv.lab1.models.XmlUser;
import dmitryv.lab1.models.XmlUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class XmlUserDetailsService implements UserDetailsService {

    private final Map<String, XmlUser> users = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader;

    @Autowired
    public XmlUserDetailsService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            JAXBContext context = JAXBContext.newInstance(XmlUsers.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = getClass().getResourceAsStream("/User.xml");
            XmlUsers usersObj = (XmlUsers) unmarshaller.unmarshal(is);
            usersObj.getUsers().forEach(userXml -> users.put(userXml.getUsername(), userXml));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users from XML", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        XmlUser userXml = users.get(username);
        if (userXml == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(userXml.getRoles());
        return new org.springframework.security.core.userdetails.User(userXml.getUsername(), userXml.getPassword(), authorities);
    }

    public void saveUser(XmlUser newUser) {

        users.put(newUser.getUsername(), newUser);


        XmlUsers xmlUsers = new XmlUsers();
        xmlUsers.setUsers(new ArrayList<>(users.values()));


        try {
            JAXBContext context = JAXBContext.newInstance(XmlUsers.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


            FileSystemResource resource = new FileSystemResource("src/main/resources/User.xml");
            try (OutputStream os = new FileOutputStream(resource.getFile())) {
                marshaller.marshal(xmlUsers, os);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save users to XML", e);
        }
    }
}