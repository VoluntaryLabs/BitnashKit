//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWallet.h"
#import "NSObject+BNJSON.h"
#import "NSString+BNJSON.h"

@implementation BNWallet

- (id)init
{
    self = [super init];
    self.server = [[BNServer alloc] init];
    return self;
}

- (void)setPath:(NSString *)path
{
    _server.path = path;
}

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

- (BNEscrowTx *)newEscrowTx
{
    BNEscrowTx *tx = [[BNEscrowTx alloc] init];
    tx.wallet = self;
    return tx;
}

@end
