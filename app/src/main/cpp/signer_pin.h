#pragma once

#include <cstddef>
#include <cstdint>
#include "runtime_pin.h"

inline bool signerPinMatches(const std::uint8_t* digest, std::size_t size) {
    static_assert(sizeof(kMaskedSignerPin) == 32, "A SHA-256 signer pin is required");
    if (digest == nullptr || size != sizeof(kMaskedSignerPin)) return false;
    unsigned int difference = 0;
    for (std::size_t i = 0; i < size; ++i) {
        const auto expected = static_cast<std::uint8_t>(kMaskedSignerPin[i] ^ ((0xA7 + i * 13) & 0xff));
        difference |= digest[i] ^ expected;
    }
    return difference == 0;
}
