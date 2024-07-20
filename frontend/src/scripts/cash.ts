export interface CashHistoryEntry {
    username: string,
    date: number,
    amount: number,
    type: string,
    note: string
}

export function currentCash(): number {
    return Math.round(Math.random() * 100000) / 100
}

export function cashHistory(limit: number = 100): CashHistoryEntry[] {
    let res: CashHistoryEntry[] = []
    for (let i = 0; i < limit; i++) {
        res.push(
            {
                username: 'someone',
                date: Math.floor(Date.now() / 1000),
                amount: Math.round(Math.random() * 100 - 50) / 100,
                type: 'pledge',
                note: 'money \\o/. No seriously there has to be a reason. Sometimes even a very loooooong reason.'
            }
        )
    }
    return res
}
