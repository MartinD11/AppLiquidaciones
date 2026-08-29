package com.liquidacionremates.app.Service;

import com.liquidacionremates.app.Repository.ClientRepository;
import com.liquidacionremates.app.dto.ClientDTO;
import com.liquidacionremates.app.entity.Client;
import com.liquidacionremates.app.exception.ResourceNotFoundException;
import com.liquidacionremates.app.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    @Override
    public ClientDTO findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));

        return clientMapper.toClientDTO(client);
    }

    @Override
    public List<ClientDTO> findAll() {
        return clientRepository.findByActiveTrue().stream()
                .map(client-> clientMapper.toClientDTO(client))
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO save(ClientDTO clientDTO) {
        Client client = clientMapper.toEntity(clientDTO);
        return clientMapper.toClientDTO(clientRepository.save(client));
    }

    @Override
    public ClientDTO update(Long id ,ClientDTO clientDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));

        if(clientDTO.getName() != null) client.setName(clientDTO.getName());
        if(clientDTO.getLastName() != null) client.setLastName(clientDTO.getLastName());

        return clientMapper.toClientDTO(clientRepository.save(client));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));

        client.setActive(false);
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    @Override
    public ClientDTO findByNameAndLastName(String name, String lastName) {
        Client client = clientRepository.findByNameAndLastName(name,lastName)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with name: " + name));

        return clientMapper.toClientDTO(client);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientDTO> searchByNameOrLastName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<Client> clients = clientRepository.searchByFullName(query);

        return clients.stream()
                .map(client -> clientMapper.toClientDTO(client))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDTO> searchHistorical(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return clientRepository.searchHistoricalByFullName(query).stream()
                .map(client -> clientMapper.toClientDTO(client))
                .collect(Collectors.toList());
    }
}
