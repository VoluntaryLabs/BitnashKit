//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWallet.h"
#import "BNTx.h"
#import "NSObject+BNJSON.h"
#import "NSString+BNJSON.h"

@implementation BNWallet

+ (NSArray *)jsonProperties
{
    return [NSArray array];
}

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

- (BNError *) error
{
    return _server.error;
}

- (NSNumber *)balance
{
    return [_server sendMessage:@"balance" withObject:self withArg:nil];
}

- (BNKey *)createKey
{
    return [_server sendMessage:@"createKey" withObject:self withArg:nil];
}

- (BNTx *)newTx
{
    BNTx *tx = [[BNTx alloc] init];
    tx.wallet = self;
    return tx;
}

- (NSArray *)transactions
{
    return [_server sendMessage:@"transactions" withObject:self];
}

- (NSArray *)keys
{
    return [_server sendMessage:@"keys" withObject:self];
}

@end
