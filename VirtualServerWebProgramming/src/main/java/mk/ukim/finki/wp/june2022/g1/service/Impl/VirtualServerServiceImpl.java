package mk.ukim.finki.wp.june2022.g1.service.Impl;

import mk.ukim.finki.wp.june2022.g1.model.OSType;
import mk.ukim.finki.wp.june2022.g1.model.User;
import mk.ukim.finki.wp.june2022.g1.model.VirtualServer;
import mk.ukim.finki.wp.june2022.g1.model.exceptions.InvalidUserIdException;
import mk.ukim.finki.wp.june2022.g1.model.exceptions.InvalidVirtualMachineIdException;
import mk.ukim.finki.wp.june2022.g1.repository.UserRepository;
import mk.ukim.finki.wp.june2022.g1.repository.VirtualServerRepository;
import mk.ukim.finki.wp.june2022.g1.service.VirtualServerService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VirtualServerServiceImpl implements VirtualServerService {

    private final VirtualServerRepository virtualServerRepository;
    private final UserRepository userRepository;

    public VirtualServerServiceImpl(VirtualServerRepository virtualServerRepository, UserRepository userRepository) {
        this.virtualServerRepository = virtualServerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<VirtualServer> listAll() {
        return this .virtualServerRepository.findAll();
    }

    @Override
    public VirtualServer findById(Long id) {
        return this.virtualServerRepository.findById(id).orElseThrow(InvalidVirtualMachineIdException::new);
    }

    @Override
    public VirtualServer create(String name, String ipAddress, OSType osType, List<Long> owners, LocalDate launchDate) {
        return this.virtualServerRepository.save(new VirtualServer(name, ipAddress, osType, this.userRepository.findAllById(owners), launchDate));
    }

    @Override
    public VirtualServer update(Long id, String name, String ipAddress, OSType osType, List<Long> owners) {
        VirtualServer virtualServer = this.virtualServerRepository.findById(id).orElseThrow(InvalidVirtualMachineIdException::new);
        virtualServer.setInstanceName(name);
        virtualServer.setIpAddress(ipAddress);
        virtualServer.setOSType(osType);
        virtualServer.setOwners(this.userRepository.findAllById(owners));
        return this.virtualServerRepository.save(virtualServer);
    }

    @Override
    public VirtualServer delete(Long id) {
        VirtualServer virtualServer = this.findById(id);
        this.virtualServerRepository.delete(virtualServer);
        return virtualServer;
    }

    @Override
    public VirtualServer markTerminated(Long id) {
        VirtualServer setMarkTerminated = this.virtualServerRepository.findById(id).orElseThrow(InvalidVirtualMachineIdException::new);
        setMarkTerminated.setTerminated(setMarkTerminated.getTerminated());
        return this.virtualServerRepository.save(setMarkTerminated);
    }

    @Override
    public List<VirtualServer> filter(Long ownerId, Integer activeMoreThanDays) {
        if(ownerId == null && activeMoreThanDays == null){
            return this.virtualServerRepository.findAll();
        }
        else if(ownerId == null){
            return this.virtualServerRepository.findAll().stream().filter(virtualServer -> LocalDate.now().getDayOfYear() - virtualServer.getLaunchDate().getDayOfYear() > activeMoreThanDays).collect(Collectors.toList());
        }
        else if(activeMoreThanDays == null){
            User user = this.userRepository.findById(ownerId).orElseThrow(InvalidUserIdException::new);
            return this.virtualServerRepository.findAllByOwnersContaining(user);
        }
        else {
            User user = this.userRepository.findById(ownerId).orElseThrow(InvalidUserIdException::new);
            return this.virtualServerRepository.findAllByOwnersContaining(user).stream().filter(virtualServer -> LocalDate.now().getDayOfYear() - virtualServer.getLaunchDate().getDayOfYear() > activeMoreThanDays).collect(Collectors.toList());
        }
        //return new ArrayList<>();
    }
}