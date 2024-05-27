package com.worldline.easypay.posref.control;

import java.util.List;

import org.springframework.stereotype.Service;

import com.worldline.easypay.posref.boundary.PosRefResponse;
import com.worldline.easypay.posref.entity.PosRef;
import com.worldline.easypay.posref.entity.PosRefRepository;

@Service
public class PosService {
    
    private PosRefRepository repository;

    public PosService(PosRefRepository repository) {
        this.repository = repository;
    }

    public List<PosRefResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private PosRefResponse toResponse(PosRef posRef) {
        return new PosRefResponse(posRef.posId, posRef.location, posRef.active);
    }
}
