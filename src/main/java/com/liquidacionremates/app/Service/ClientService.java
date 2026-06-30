package com.liquidacionremates.app.Service;


import com.liquidacionremates.app.dto.ClientDTO;

import java.util.List;

public interface ClientService {
    ClientDTO findById(Long id);
    List<ClientDTO> findAll();
    ClientDTO save (ClientDTO clientDTO);
    ClientDTO update(Long id,ClientDTO clientDTO);
    void delete(Long id);
    ClientDTO findByNameAndLastName(String name,String lastName);
}
