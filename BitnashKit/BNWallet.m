//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWallet.h"

@implementation BNWallet


- (NSError *) error
{
    return _server.error;
}

- (NSNumber *)balance
{
    return [_server sendMessage:@"getBalance" withObject:nil];
}

- (NSString *)createAddress
{
    return [_server sendMessage:@"createAddress" withObject:nil];
}

- (BNEscrowTx *)newEscrowTransaction
{
    BNEscrowTx *tx = [[BNEscrowTx alloc] init];
    tx.wallet = self;
    return tx;
}

@end
