package dev.chojo.simplepos.service.storage;

import dev.chojo.simplepos.entity.Storage;

public class StorageTransaction {
    Storage storage;
    int amount = 0;

    public StorageTransaction() {
    }

    public void update(Storage storage){
        this.storage = storage;
        amount +=1;
    }

    public Storage getStorage() {
        return storage;
    }

    public int getAmount() {
        return amount;
    }
}
