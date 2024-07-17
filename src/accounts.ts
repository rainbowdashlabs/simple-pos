interface LazyAccount {
    id: number,
    name: string;
    balance: number;
}

interface FullAccount extends LazyAccount {

}

const names = ["John", "Jane", "Peter", "Paul", "Jennifer", "Mark", "Emma", "Robert", "Linda", "Michael"];
const surnames = ["Smith", "Johnson", "Williams", "Brown", "Taylor", "Miller", "Wilson", "Moore", "Davis", "Clark"];

function getRandomItem(array) {
    return array[Math.floor(Math.random() * array.length)];
}

function randomUser(id: number): LazyAccount {
    return {
        id: id,
        name: `${getRandomItem(names)} ${getRandomItem(surnames)}`,
        balance: Math.random() * 60 - 30
    }
}

const dummy_accounts = []
for (let i = 0; i < 60; i++) {
    dummy_accounts.push(randomUser(i))
}

export function accounts(): LazyAccount[] {
    return dummy_accounts
}

export function account(id: number): FullAccount {
    let lazy = randomUser(id)
    return lazy
}
