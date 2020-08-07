def get_config(name):
    config = {
        "seq_len": 64,
        "node_len": 16,
        "batch_size": 8
    }
    return config[name]
