interface Account {
    id: number,
    name: string;
    balance: number;
}

const dummy_accounts = []
for (let i = 0; i < 60; i++) {
    dummy_accounts.push({
        id: i,
        name: `Dummy Account ${i}`,
        balance: Math.random() * 60 - 30
    })
}

export function accounts(): Account[] {
    return dummy_accounts
}

